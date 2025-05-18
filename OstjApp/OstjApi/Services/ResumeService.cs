using OstjApi.Models;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using OstjApi.Data;
using System.Collections.Generic;
using System.Reflection;
using System.Text.Json;

namespace OstjApi.Services
{
    public class PersonService(OstjDbContext dbContext, IAIClient aiClient) : IPersonService
    {
        private readonly OstjDbContext _dbContext = dbContext;
        private readonly IAIClient _aiClient = aiClient;

        public async ValueTask<Person?> GetPersonAsync(int id)
        {
            return await _dbContext.Persons.FindAsync(id);
        }

        public async Task<Person> SavePersonAsync(string fileName, string contentType, byte[] content)
        {
            var resumeText = GetResumeText(contentType, content);
            var resume = new Resume
            {
                PersonId = 0,
                Content = resumeText
            };

            var person = await GetPersonFromResume(resumeText);
            person.Resumes.Add(resume);

            try
            {
                _dbContext.Persons.Add(person);
                await _dbContext.SaveChangesAsync();
                return person;
            }
            catch (DbUpdateException ex) when (ex.InnerException?.Message.Contains("unique", StringComparison.OrdinalIgnoreCase) == true
                                             || ex.InnerException?.Message.Contains("duplicate", StringComparison.OrdinalIgnoreCase) == true)
            {
                var existingPerson = await _dbContext.Persons
                    .FirstOrDefaultAsync(p => p.Email == person.Email);

                if (existingPerson == null)
                    throw;

                return existingPerson;
            }
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
                throw new InvalidOperationException("OpenAI response does not contain valid JSON.");

            string json = responseText.Substring(startIdx, endIdx - startIdx + 1);

            var person = JsonSerializer.Deserialize<Person>(json);

            if (person == null)
                throw new InvalidOperationException("Failed to parse person info from OpenAI response.");

            return person;

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
}