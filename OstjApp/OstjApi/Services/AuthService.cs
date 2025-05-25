using Microsoft.EntityFrameworkCore;
using OstjApi.Data;
using OstjApi.Models;

namespace OstjApi.Services
{
    public class AuthService : IAuthService
    {
        private readonly OstjDbContext _dbContext;
        private readonly int _minCode;
        private readonly int _maxCode;
        private readonly int _maxGenerationAttempts = 1000;

        public AuthService(OstjDbContext dbContext, uint codeLenth = 6, uint maxGenerationAttempts = 1000)
        {
            if (codeLenth > 10)
            {
                throw new ArgumentOutOfRangeException(nameof(codeLenth), "Code length must be less than 10.");
            }
            _minCode = (int)Math.Pow(10, codeLenth - 1);
            _maxCode = (int)(Math.Pow(10, codeLenth) - 1);
            _dbContext = dbContext;
            _maxGenerationAttempts = (int)maxGenerationAttempts;
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
                        Expires = DateTime.UtcNow.AddMinutes(15)
                    };
                    _dbContext.Otcs.Add(otc);
                    await _dbContext.SaveChangesAsync();
                    return code;
                }
                catch (DbUpdateException ex) when (ex.InnerException?.Message.Contains("unique", StringComparison.OrdinalIgnoreCase) == true
                                 || ex.InnerException?.Message.Contains("duplicate", StringComparison.OrdinalIgnoreCase) == true)
                {
                }
            }
            throw new InvalidOperationException("Failed to generate a unique code after multiple attempts.");
        }

        public async Task<OtcStatus> VerifyCodeAsync(string email, string code)
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