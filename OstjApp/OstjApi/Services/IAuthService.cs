namespace OstjApi.Services
{
    public interface IAuthService
    {
        Task<string> GenerateCodeAsync(string email);
        Task<OtcStatus> ValidateCodeAsync(string email, string code);
    }
}