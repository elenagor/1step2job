using System.ComponentModel;
using Microsoft.CodeAnalysis.Options;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;
using OstjApi.Data;
using OstjApi.Models;

namespace OstjApi.Services
{
    public class OtcSettings
    {
        public int CodeLength { get; set; }
        public int CodeEpirationMinutes { get; set; }
        public int MaxGenerationAttempts { get; set; }
    }

    public class AuthService : IAuthService
    {
        private readonly ILogger<AuthService> _logger;
        private readonly OstjDbContext _dbContext;
        private readonly int _minCode;
        private readonly int _maxCode;
        private readonly int _maxGenerationAttempts;
        private readonly int _codeExpirationMinutes;

        public AuthService(OstjDbContext dbContext, IOptions<OtcSettings> options, ILogger<AuthService> logger)
        {
            var settings = options.Value;
            if (settings.CodeLength < 1 || settings.CodeLength > 10)
            {
                throw new ArgumentOutOfRangeException("Otc.CodeLenth", "Code length must have a positive value less than 10.");
            }

            if (settings.MaxGenerationAttempts < 0)
            {
                throw new ArgumentOutOfRangeException("Otc.MaxGenerationAttempts", "Max number of code generation attemps must have a positive value.");
            }

            if (settings.CodeEpirationMinutes > 0)
            {
                throw new ArgumentOutOfRangeException("Otc.CodeEpirationMinutes", "Code expiration must have greater that 1 min.");
            }

            _minCode = (int)Math.Pow(10, settings.CodeLength - 1);
            _maxCode = (int)(Math.Pow(10, settings.CodeLength) - 1);
            _dbContext = dbContext;
            _maxGenerationAttempts = settings.MaxGenerationAttempts;
            _codeExpirationMinutes = settings.CodeEpirationMinutes;

            _logger = logger;
        }

        public async Task<string> GenerateCodeAsync(string email)
        {
            // Mark all otcs with the same email as used
            await _dbContext.Otcs
                .Where(o => o.Email == email)
                .ExecuteUpdateAsync(setters => setters.SetProperty(o => o.IsUsed, true));

            for (int i = 0; i < _maxGenerationAttempts; i++)
            {
                int random = new Random().Next(_minCode, _maxCode);
                var code = random.ToString();
                try
                {
                    var otc = new Otc
                    {
                        Email = email,
                        Code = code,
                        Expires = DateTime.UtcNow.AddMinutes(_codeExpirationMinutes)
                    };
                    _dbContext.Otcs.Add(otc);
                    await _dbContext.SaveChangesAsync();
                    return code;
                }
                catch (DbUpdateException ex) when (ex.InnerException?.Message.Contains("unique", StringComparison.OrdinalIgnoreCase) == true
                                 || ex.InnerException?.Message.Contains("duplicate", StringComparison.OrdinalIgnoreCase) == true)
                {
                    _logger.LogWarning(ex, "Failed to generate a unique code for email {Email}. Attempt {Attempt} of {MaxAttempts}.", email, i + 1, _maxGenerationAttempts);
                    // Ignore the exception and try again
                }
            }
            _logger.LogError("Failed to generate a unique code for email {Email} after {MaxAttempts} attempts.", email, _maxGenerationAttempts);
            // If we reach here, it means we failed to generate a unique code
            throw new InvalidOperationException("Failed to generate a unique code after multiple attempts.");
        }

        public async Task<OtcStatus> ValidateCodeAsync(string email, string code)
        {
            var otc = await _dbContext.Otcs
                .OrderByDescending(o => o.Expires)
                .FirstOrDefaultAsync(o => o.Email == email && o.Code == code);

            if (otc == null)
            {
                return OtcStatus.NotFound;
            }
            if (otc.IsUsed)
            {
                return OtcStatus.Used;
            }
            if (otc.Expires < DateTime.UtcNow)
            {
                return OtcStatus.Expired;
            }
            otc.IsUsed = true;
            _dbContext.Otcs.Update(otc);
            await _dbContext.SaveChangesAsync();
            return OtcStatus.Valid;
        }
    }

    public enum OtcStatus
    {
        NotFound,
        Expired,
        Used,
        Valid
    }

}