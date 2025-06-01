using System.ClientModel;
using Microsoft.Extensions.Options;
using OpenAI;
using OpenAI.Chat;
using OpenAI.Embeddings;

namespace OstjApi.Services
{
    public class AIClientSettings
    {
        public required string ApiUri { get; set; }
        public required string ApiKey { get; set; }
        public required string Model { get; set; }
    }

    public class AIClient : IAIClient
    {
        private readonly ChatClient _chatClient;
        private readonly EmbeddingClient _embeddingClient;
        private readonly ILogger<AIClient> _logger;

        public AIClient(IOptions<AIClientSettings> settings, ILogger<AIClient> logger)
        {
            _logger = logger;

            var model = settings.Value.Model;
            var uri = settings.Value.ApiUri;
            var apiKey = settings.Value.ApiKey;

            if (string.IsNullOrEmpty(model))
            {
                throw new ArgumentNullException("OpenAI.Model", "Model cannot be null or empty.");
            }

            if (string.IsNullOrEmpty(uri))
            {
                throw new ArgumentNullException("OpenAI.ApiUri", "Uri cannot be null or empty.");
            }

            if (string.IsNullOrEmpty(apiKey))
            {
                throw new ArgumentNullException("OpenAI.ApiKey", "ApiKey cannot be null or empty.");
            }

            OpenAIClientOptions options = new OpenAIClientOptions()
            {
                Endpoint = new Uri(uri)
            };
            _chatClient = new(model, new ApiKeyCredential(apiKey), options);
            _embeddingClient = new(model, new ApiKeyCredential(apiKey), options);
        }

        public async Task<float[]> GenerateEmbeddingAsync(string text)
        {
            var embedding = await _embeddingClient.GenerateEmbeddingAsync(text);
            return embedding.Value.ToFloats().ToArray();
        } 

        public async Task<string> RunPromptAsync(string prompt)
        {
            var requestBody = new { prompt };
            ChatCompletion completion = await _chatClient.CompleteChatAsync(prompt);
            return completion.Content[0].Text;
        }
    }
}