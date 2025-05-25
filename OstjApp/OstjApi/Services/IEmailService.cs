namespace OstjApi.Services
{
    public interface IEmailService
    {
        Task SendOtcEmailAsync(string email, string code);
    }
}