using System.ClientModel;
using OpenAI;
using OpenAI.Chat;

namespace OstjApi.Services
{
    public class AIClient : IAIClient
    {
        private readonly ChatClient _client;

        public AIClient(string model, string uri, string apiKey)
        {
            OpenAIClientOptions options = new OpenAIClientOptions()
            {
                Endpoint = new Uri(uri)
            };
            _client = new(model, new ApiKeyCredential(apiKey), options);
        }


        public async Task<string> RunPromptAsync(string prompt)
        {
            var requestBody = new { prompt };
            ChatCompletion completion = await _client.CompleteChatAsync(prompt);
            return completion.Content[0].Text;
        }
    }
}