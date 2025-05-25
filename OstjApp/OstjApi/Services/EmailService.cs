using System.Net;
using System.Net.Mail;
using System.Net.Security;

namespace OstjApi.Services
{

    public class EmailService(string smtpServer, int port, string username, string password, bool ssl = true) : IEmailService
    {
        private readonly string _smtpServer = smtpServer;
        private readonly int _port = port;
        private readonly string _username = username;
        private readonly string _password = password;
        private readonly bool _ssl = ssl;

        public async Task SendOtcEmailAsync(string email, string code)
        {
            var subject = "Your One-Time Code";
            var message = $"Your one-time code is: {code}";

            await SendEmailAsync(email, subject, message);
        }

        async Task SendEmailAsync(string email, string subject, string message)
        {
            var smtpClient = new SmtpClient(_smtpServer, _port)
            {
                Credentials = new NetworkCredential(_username, _password),
                EnableSsl = _ssl,
            };

            var mailMessage = new MailMessage
            {
                From = new MailAddress(_username),
                Subject = subject,
                Body = message,
                IsBodyHtml = true,
            };

            mailMessage.To.Add(email);

            await smtpClient.SendMailAsync(mailMessage);
        }
    }
}