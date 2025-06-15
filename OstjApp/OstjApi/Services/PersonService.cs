using OstjApi.Models;
using Microsoft.EntityFrameworkCore;
using OstjApi.Data;
using System.Reflection;
using System.Text.Json;
using Ostj.Constants;
using Ostj.Shared.Contracts;
using Humanizer;


namespace OstjApi.Services
{
    public class PersonService(OstjDbContext dbContext, IAIClient aiClient, ILogger<PersonService> logger) : IPersonService
    {
        private readonly OstjDbContext _dbContext = dbContext;
        private readonly IAIClient _aiClient = aiClient;
        private readonly ILogger<PersonService> _logger = logger;

        #region IPersonService Implementation
        public async ValueTask<Person?> GetPersonAsync(int id)
        {
            var person = await _dbContext.Persons
                .Include(p => p.Profiles)
                .FirstOrDefaultAsync(p => p.Id == id);
            if (person == null)
            {
                _logger.LogError("Person with id {id} is not found", id);
                return null;
            }
            return person;
        }

        public async ValueTask<ProfileDetails?> GetProfileDetailsAsync(int personId, int profileId)
        {
            return await _dbContext.Profiles
                .Where(p => p.PersonId == personId && p.Id == profileId)
                .Include(p => p.JobTitles)
                .FirstOrDefaultAsync()
                   ?? throw new InvalidOperationException($"Profile with ID {profileId} for person with ID {personId} does not exist.");
        }

        public async Task<int> SaveProfileFromResumeAsync(int personId, string fileName, string contentType, byte[] content)
        {
            var person = await _dbContext.Persons.FindAsync(personId) ?? throw new InvalidOperationException($"Person with ID {personId} does not exist.");
            var resumeText = GetResumeText(contentType, content);
            var inferedPerson = await GetPersonFromResume(resumeText);

            if (string.IsNullOrEmpty(person.Name) && !string.IsNullOrEmpty(inferedPerson.Name))
            {
                person.Name = inferedPerson.Name;
            }

            if (inferedPerson.Profiles != null && inferedPerson.Profiles.Count == 1)
            {
                person.Profiles.Add(inferedPerson.Profiles[0]);
            }

            _dbContext.Persons.Update(person);
            await _dbContext.SaveChangesAsync();
            return person.Profiles.Last().Id;
        }

        public async Task SaveProfileAsync(int personId, ProfileDetails profile)
        {
            if (profile == null)
                throw new ArgumentNullException(nameof(profile), "Profile cannot be null.");
            if (personId <= 0)
                throw new ArgumentOutOfRangeException(nameof(personId), "Person ID must be greater than zero.");

            foreach (var jobTitle in profile.JobTitles)
            {
                if (jobTitle.JobTitleDetails != null && jobTitle.JobTitleDetails.Embedding == null)
                {
                    float[] embedding = await GetEmbeddingAsync(jobTitle.Title);
                    jobTitle.JobTitleDetails.Embedding = new Pgvector.Vector(embedding);
                }
            }

            _dbContext.Profiles.Update(profile);
            await _dbContext.SaveChangesAsync();
        }

        public async ValueTask<List<PersonPositionMatch>> GetPositionsForProfile(int personId, int profileId)
        {
            var positions = await _dbContext.PersonPositionMatches
                .Include(p => p.Position)
                .Where(x => x.PersonId == personId && x.ProfileId == profileId)
                .ToListAsync();

            if (positions == null || positions.Count == 0)
            {
                return [
                    new PersonPositionMatch() {
                        Id = 1,
                        PersonId = 1,
                        Person = null,
                        PositionId = 1,
                        Score = 9,
                        Date = DateTime.Now.AddDays(-1),

                        Position = new Position() {
                            Id = 1,
                            Title = "Software Engineer",
                            ExternalId = "1",
                            Location = new OstjApi.Models.Location() {
                                Country = "US",
                                StateOrRegion = "PA",
                                City = "Pittsburgh",
                            },
                            IsRemote = null,
                            Description = """
                            This company, a leading project developer with a focus on renewable energy and infrastructure serving utilities, businesses, and communities, is dedicated to generating value through investments in sustainable energy infrastructure for its partners, stakeholders, energy consumers, and local communities. Our client actively engages in a wide range of industries and commercial ventures, providing innovative, integrated business solutions to clients worldwide through a global network. As the Director of Project Development, you will oversee the company's portfolio of solar projects by coordinating and reviewing the work of various development functions, consultants, and other team members who are part of a remote, distributed workforce. To accomplish 
                            the project and business goal, this role will collaborate closely with the interconnectivity, permitting, real estate, and engineering teams. 

                            Join a workplace where brilliant individuals with a desire for challenge, creativity, and passion propagate their goals around the globe. 

                            Key responsibilities for this role will include (but not limited to): 
                            • Work with the VP on the company’s overall strategy and development efforts in the Midwest or East Region 
                            • Oversee the processes for zoning, environmental protection, permitting, entitlements for land use, and compliance. 
                            • Identify, oversee, and lead collaboration partners and outside consultants. 
                            • Create workable, economically sound new and ongoing solar projects. 
                            • Establish a target scope of work and monitor project progress to ensure it is on time and on budget. 
                            • Supervise the development of solar energy projects from the feasibility stage through the start of construction. 
                            • Stay up to date on U.S. renewable energy opportunities as well as national industry trends that may have an impact on future 
                            solar and renewable energy development opportunities and their competitiveness. 

                            Required and desired skillsets, experience and qualifications for this role: 
                            • Bachelor's Degree is necessary. 
                            • Experience with project development for at least 8-10 years, preferably in the solar, renewable energy, or real estate 
                            development industries 
                            • Experience with team building and leadership 
                            • Skilled in project management with the ability to identify and resolve potential project flaws and prioritize critical path tasks 
                            • Strong project management background with the capacity to spot potential project defects, address them, and determine the 
                            tasks that should be prioritized on the critical path. 
                            • Knowledge of market trends for renewable energy sources and how they affect the development of solar projects. 
                            • Capability to manage many priorities concurrently and flourish in rapidly changing environments 
                            • Ability to utilize Excel spreadsheets, create excel trackers and be able to use Word and Powerpoint for reporting/presentations 
                            via teams or in person                            
                        """,
                        ApplyUrl = "https://somecompany.com",
                        SalaryMin = 100000,
                        SalaryMax = 200000,
                        }
                    }
                ];

            }
            return positions;
        }

        #endregion

        #region Private Methods
        private async Task<Person> GetPersonFromResume(string resumeText)
        {
            string promptTemplate;
            var resourceName = "OstjApi.Resources.ExtractPersonInfoPrompt.txt";
            using (var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream(resourceName))
            using (var reader = new StreamReader(stream ?? throw new InvalidOperationException("Prompt resource not found.")))
            {
                promptTemplate = await reader.ReadToEndAsync();
            }

            // Replace {resume} placeholder
            string prompt = promptTemplate.Replace("{resume}", resumeText);

            var openAiResponse = await _aiClient.RunPromptAsync(prompt);

            // Extract JSON from response
            string responseText = openAiResponse.Trim();
            int startIdx = responseText.IndexOf('{');
            int endIdx = responseText.LastIndexOf('}');
            if (startIdx == -1 || endIdx == -1 || endIdx <= startIdx)
                throw new InvalidOperationException("Provided document does not contain valid person resume information.");

            string json = responseText.Substring(startIdx, endIdx - startIdx + 1);

            var personInfo = JsonSerializer.Deserialize<InferredPersonInfo>(json);

            if (personInfo == null
                || (string.IsNullOrEmpty(personInfo.Name)
                    && string.IsNullOrEmpty(personInfo.Name)
                    && (personInfo.JobTitles == null || personInfo.JobTitles.Length == 0)))
                throw new InvalidOperationException("Provided document does not contain valid person resume information.");

            var jobTitles = await GetJobTitlesFromPersonInfo(personInfo);

            var profiles = new List<Profile>
            {
                new() {
                    PersonId = 0, // Will be set after saving
                    Name = Constants.DefaultProfileName,
                    ProfileDetails = new ProfileDetails
                    {
                        PersonId = 0, // Will be set after saving
                        Name = Constants.DefaultProfileName,
                        AcceptRemote = true,
                        Location = new (),
                        SalaryMin = null,
                        SalaryMax = null,
                        ExtraRequirements = null,
                        Resume = resumeText,
                        JobTitles = jobTitles
                    }
                }
            };

            var person = new Person
            {
                Name = personInfo.Name ?? string.Empty,
                Email = personInfo.Email ?? string.Empty,
                Phone = personInfo.Phone,
                EnrollmentType = EnrollmentType.NotEnrolled,
                Profiles = profiles,
            };

            return person;
        }

        private async Task<List<JobTitle>> GetJobTitlesFromPersonInfo(InferredPersonInfo personInfo)
        {
            var jobTitles = new List<JobTitle>();
            if (personInfo.JobTitles == null || personInfo.JobTitles.Length == 0)
                return jobTitles;

            foreach (string jobTitle in personInfo.JobTitles)
            {
                if (string.IsNullOrWhiteSpace(jobTitle))
                    continue;
                var embedding = await GetEmbeddingAsync(jobTitle);
                jobTitles.Add(new JobTitle
                {
                    Title = jobTitle,
                    JobTitleDetails = new JobTitleDetails
                    {
                        Title = jobTitle,
                        Embedding = new Pgvector.Vector(embedding),
                        IsUserDefined = false
                    },
                });
            }
            return jobTitles;
        }

        private async Task<float[]> GetEmbeddingAsync(string text)
        {
            if (string.IsNullOrWhiteSpace(text))
                return [];

            return await _aiClient.GenerateEmbeddingAsync(text.ToUpper());
        }

        private static string GetResumeText(string contentType, byte[] content)
        {
            if (contentType == System.Net.Mime.MediaTypeNames.Text.Plain)
            {
                return content.Length > 0 ? System.Text.Encoding.UTF8.GetString(content) : string.Empty;
            }
            throw new NotImplementedException();
        }
        #endregion
    }

    class InferredPersonInfo
    {
        public string? Name { get; set; }
        public string? Email { get; set; }
        public string? Phone { get; set; }
        public string[]? JobTitles { get; set; } = [];
    }
}