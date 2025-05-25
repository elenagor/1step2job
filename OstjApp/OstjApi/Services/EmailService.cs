using System.Net;
using System.Net.Mail;
using System.Net.Security;
using Microsoft.Extensions.Options;

namespace OstjApi.Services
{
    public class EmailSettings
    {
        public string SmtpServer { get; set; } = string.Empty;
        public int Port { get; set; } = 587;
        public string Username { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
        public bool Ssl { get; set; } = true;
    }

    public class EmailService(IOptions<EmailSettings> options, ILogger<EmailService> logger) : IEmailService
    {
        private readonly ILogger<EmailService> _logger = logger;
        private readonly EmailSettings _emailSettings = options.Value;

        public async Task SendOtcEmailAsync(string email, string code)
        {
            var subject = "Your One-Time Code";
            var message = $"Your one-time code is: {code}";

            await SendEmailAsync(email, subject, message);
        }

        async Task SendEmailAsync(string email, string subject, string message)
        {
            var smtpClient = new SmtpClient(_emailSettings.SmtpServer, _emailSettings.Port)
            {
                Credentials = new NetworkCredential(_emailSettings.Username, _emailSettings.Password),
                EnableSsl = _emailSettings.Ssl,
            };

            var mailMessage = new MailMessage
            {
                From = new MailAddress(_emailSettings.Username),
                Subject = subject,
                Body = message,
                IsBodyHtml = true,
            };

            mailMessage.To.Add(email);

            await smtpClient.SendMailAsync(mailMessage);
            _logger.LogDebug("OTC Email sent to {Email}", email);
        }
    }
}