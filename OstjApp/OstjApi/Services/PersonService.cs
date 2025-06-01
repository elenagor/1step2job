using OstjApi.Models;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using OstjApi.Data;
using System.Collections.Generic;
using System.Reflection;
using System.Text.Json;
using OstjLib.Contracts;

namespace OstjApi.Services
{
    public class PersonService(OstjDbContext dbContext, IAIClient aiClient, ILogger<PersonService> logger) : IPersonService
    {
        private readonly OstjDbContext _dbContext = dbContext;
        private readonly IAIClient _aiClient = aiClient;
        private readonly ILogger<PersonService> _logger = logger;

        public async ValueTask<Person?> GetPersonAsync(int id)
        {
            return await _dbContext.Persons.FindAsync(id);
        }

        public async Task<Person> SaveProfileFromResumeAsync(int personId, string fileName, string contentType, byte[] content)
        {
            var person = await _dbContext.Persons.FindAsync(personId) ?? throw new InvalidOperationException($"Person with ID {personId} does not exist.");
            var resumeText = GetResumeText(contentType, content);
            var inferedPerson = await GetPersonFromResume(resumeText);

            if (string.IsNullOrEmpty(person.Name) && !string.IsNullOrEmpty(inferedPerson.Name))
            {
                person.Name = inferedPerson.Name;
            }

            if (inferedPerson.Profiles != null && inferedPerson.Profiles.Count > 0)
            {
                foreach (var profile in inferedPerson.Profiles)
                {
                    person.Profiles.Add(inferedPerson.Profiles[0]);
                }
            }

            _dbContext.Persons.Update(person);
            await _dbContext.SaveChangesAsync();
            return person;
        }

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

            var personInfo = JsonSerializer.Deserialize<PersonInfo>(json);

            if (personInfo == null
                || (string.IsNullOrEmpty(personInfo.Name)
                    && string.IsNullOrEmpty(personInfo.Name)
                    && (personInfo.JobTitles == null || personInfo.JobTitles.Count == 0)))
                throw new InvalidOperationException("Provided document does not contain valid person resume information.");

            var jobTitles = await GetJobTitlesFromPersonInfo(personInfo);

            var profiles = new List<Profile>
            {
                new() {
                    PersonId = 0, // Will be set after saving
                    Resume = resumeText,
                    JobTitles = jobTitles
                }
            };

            var person = new Person
            {
                Name = personInfo.Name,
                Email = personInfo.Email,
                Phone = personInfo.Phone,
                EnrollmentType = EnrollmentType.NotEnrolled,
                Profiles = profiles,
            };

            return person;
        }

        private async Task<List<JobTitle>> GetJobTitlesFromPersonInfo(PersonInfo personInfo)
        {
            var jobTitles = new List<JobTitle>();
            if (personInfo.JobTitles == null || personInfo.JobTitles.Count == 0)
                return jobTitles;

            foreach (string jobTitle in personInfo.JobTitles)
            {
                if (string.IsNullOrWhiteSpace(jobTitle))
                    continue;
                var embedding = await _aiClient.GenerateEmbeddingAsync(jobTitle);
                jobTitles.Add(new JobTitle
                {
                    Title = jobTitle,
                    Embedding = new Pgvector.Vector(embedding),
                    IsUserDefined = false
                });
            }
            return jobTitles;
        }

        private static string GetResumeText(string contentType, byte[] content)
        {
            if (contentType == System.Net.Mime.MediaTypeNames.Text.Plain)
            {
                return content.Length > 0 ? System.Text.Encoding.UTF8.GetString(content) : string.Empty;
            }
            throw new NotImplementedException();
        }
    }

    public class PersonInfo
    {
        public string Name { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Phone { get; set; } = string.Empty;
        public IList<string> JobTitles { get; set; } = [];
    }

}