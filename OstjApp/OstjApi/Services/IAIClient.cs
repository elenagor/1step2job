namespace OstjApi.Services
{
    public interface IAIClient
    {
        Task<string> RunPromptAsync(string prompt);
        Task<float[]> GenerateEmbeddingAsync(string text);
    }
}