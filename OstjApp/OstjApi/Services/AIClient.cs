using System.ClientModel;
using Microsoft.Extensions.Options;
using OpenAI;
using OpenAI.Chat;
using OpenAI.Embeddings;

namespace OstjApi.Services
{
    public class AIClientSettings
    {
        public required AIEndpointSettings Completions { get; set; }
        public required AIEndpointSettings Embeddings { get; set; }
    }

    public class AIEndpointSettings
    {
        public required string ApiUri { get; set; }
        public required string ApiKey { get; set; }
        public required string Model { get; set; }
    }

    public class AIClient : IAIClient
    {
        private readonly AIEndpointSettings _completionsSettings;
        private readonly AIEndpointSettings _embeddingSettings;
        private readonly ChatClient _chatClient;
        private readonly EmbeddingClient _embeddingClient;
        private readonly ILogger<AIClient> _logger;

        public AIClient(IOptions<AIClientSettings> settings, ILogger<AIClient> logger)
        {
            _logger = logger;
            _completionsSettings = settings.Value.Completions;
            _embeddingSettings = settings.Value.Embeddings;

            if (string.IsNullOrEmpty(_completionsSettings.Model) || string.IsNullOrEmpty(_embeddingSettings.Model))
            {
                throw new ArgumentNullException("OpenAI.Model", "Model cannot be null or empty.");
            }

            if (string.IsNullOrEmpty(_completionsSettings.ApiUri) || string.IsNullOrEmpty(_embeddingSettings.ApiUri))
            {
                throw new ArgumentNullException("OpenAI.ApiUri", "Uri cannot be null or empty.");
            }

            if (string.IsNullOrEmpty(_completionsSettings.ApiKey) || string.IsNullOrEmpty(_embeddingSettings.ApiKey))
            {
                throw new ArgumentNullException("OpenAI.ApiKey", "ApiKey cannot be null or empty.");
            }

            _chatClient = new(_completionsSettings.Model, new ApiKeyCredential(_completionsSettings.ApiKey), new OpenAIClientOptions
            {
                Endpoint = new Uri(_completionsSettings.ApiUri)
            });
            _embeddingClient = new(_embeddingSettings.Model, new ApiKeyCredential(_embeddingSettings.ApiKey), new OpenAIClientOptions
            {
                Endpoint = new Uri(_embeddingSettings.ApiUri)
            });
        }

        public async Task<float[]> GenerateEmbeddingAsync(string text)
        {
            var embedding = await _embeddingClient.GenerateEmbeddingAsync(text);
            return embedding.Value.ToFloats().ToArray();
        }

        public async Task<string> RunPromptAsync(string prompt)
        {
            var requestBody = new { prompt };
            try
            {
                ChatCompletion completion = await _chatClient.CompleteChatAsync(prompt);
                return completion.Content[0].Text;
            }
            catch (Exception e)
            {
                _logger.LogError(e, $"Error while trying to run prompt");
                return string.Empty;
            }
        }
    }
}