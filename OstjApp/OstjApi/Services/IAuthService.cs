namespace OstjApi.Services
{
    public interface IAuthService
    {
        Task<string> GenerateCodeAsync(string email);
        Task<OtcStatus> VerifyCodeAsync(string email, string code);
    }
}