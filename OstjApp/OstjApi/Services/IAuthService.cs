namespace OstjApi.Services
{
    public interface IAuthService
    {
        Task<string> GenerateCodeAsync(string email);
        Task<AuthResult> ValidateCodeAsync(string email, string code);
    }
}