using System.Data.Common;
using System.Runtime.InteropServices;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Diagnostics;
using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.EntityFrameworkCore.Storage;
using Microsoft.Extensions.Logging;
using Moq;
using OstjApi.Data;
using OstjApi.Models;
using OstjApi.Services;

namespace OstjApi.Tests.Services
{
    public class AuthServiceTest : IDisposable
    {
        private readonly DbConnection _connection;
        private readonly DbContextOptions<OstjDbContext> _contextOptions;

        #region ConstructorAndDispose
        public AuthServiceTest()
        {
            _connection = new SqliteConnection("Filename=:memory:");
            _connection.Open();
            _contextOptions = new DbContextOptionsBuilder<OstjDbContext>()
                .LogTo(Console.WriteLine, LogLevel.Trace)
                .EnableSensitiveDataLogging()
                .UseSqlite(_connection)
                .Options;
        }

        OstjDbContext CreateContext() => new OstjDbContext(_contextOptions);
        public void Dispose()
        {
            _connection.Dispose();
        }
        #endregion

        [Theory]
        [InlineData("aaa@gbb.com", "123456", OtcStatus.Valid)]
        [InlineData("ccc@gbb.com", "123456", OtcStatus.Expired)]
        [InlineData("ddd@gbb.com", "123456", OtcStatus.Used)]
        [InlineData("zzz@gbb.com", "123456", OtcStatus.NotFound)]
        public async Task TestVerifyCode(string email, string code, OtcStatus expetedStatus)
        {
            using var dbContext = CreateContext();
            dbContext.Database.EnsureDeleted();
            dbContext.Database.EnsureCreated();
            dbContext.Otcs.Add(new Otc { Email = "aaa@gbb.com", Code = "123456", Expires = DateTime.UtcNow.AddDays(1) });
            dbContext.Otcs.Add(new Otc { Email = "ccc@gbb.com", Code = "123456", Expires = DateTime.UtcNow.AddMinutes(-5) });
            dbContext.Otcs.Add(new Otc { Email = "ddd@gbb.com", Code = "123456", Expires = DateTime.UtcNow.AddDays(1), IsUsed = true });
            dbContext.SaveChanges();

            var authService = new AuthService(dbContext);

            var result = await authService.VerifyCodeAsync(email, code);
            Assert.Equal(expetedStatus, result);
        }

        [Fact]
        public async Task TestGenerateCode()
        {
            using var dbContext = CreateContext();
            dbContext.Database.EnsureDeleted();
            dbContext.Database.EnsureCreated();

            var authService = new AuthService(dbContext);

            const string email = "aaa@bbb.com";
            var code = await authService.GenerateCodeAsync(email);
            var codes = await dbContext.Otcs.Where(o => o.Email == email).ToListAsync();
            Assert.Single(codes);
            Assert.Equal(email, codes[0].Email);
            Assert.Equal(6, codes[0].Code.Length);
            Assert.False(codes[0].IsUsed);
            Assert.True(codes[0].Expires > DateTime.UtcNow);
            Assert.True(codes[0].Expires < DateTime.UtcNow.AddMinutes(15));
        }

        [Fact]
        public async Task TestOldCodesInvalidated()
        {
            const string email = "aaa@bbb.com";
            string code;
            using (var dbContext = CreateContext())
            {
                dbContext.Database.EnsureDeleted();
                dbContext.Database.EnsureCreated();

                dbContext.Otcs.Add(new Otc { Email = email, Code = "123456", Expires = DateTime.UtcNow.AddDays(1) });
                dbContext.Otcs.Add(new Otc { Email = email, Code = "654321", Expires = DateTime.UtcNow.AddDays(1) });
                dbContext.SaveChanges();
                var authService = new AuthService(dbContext);

                code = await authService.GenerateCodeAsync(email);

            }

            using (var dbContext = CreateContext())
            {
                var codes = await dbContext.Otcs.Where(o => o.Email == email).ToListAsync();
                Assert.Equal(3, codes.Count);
                Assert.True(codes.First(o => o.Email == email && o.Code == "123456").IsUsed);
                Assert.True(codes.First(o => o.Email == email && o.Code == "654321").IsUsed);
                Assert.False(codes.First(o => o.Email == email && o.Code == code).IsUsed);
            }
        }

        [Fact]
        public async Task TestNoAvailableCodes()
        {
            const string email = "aaa@bbb.com";
            using (var dbContext = CreateContext())
            {
                dbContext.Database.EnsureDeleted();
                dbContext.Database.EnsureCreated();

                for (int i = 1; i < 9; i++)
                {
                    dbContext.Otcs.Add(new Otc { Email = email, Code = i.ToString(), Expires = DateTime.UtcNow.AddDays(1) });
                }
                await dbContext.SaveChangesAsync();
            }

            using (var dbContext = CreateContext())
            {
                var authService = new AuthService(dbContext, 1, 10);
                var exception = await Assert.ThrowsAsync<InvalidOperationException>(async () => await authService.GenerateCodeAsync(email));
                Assert.Equal("Failed to generate a unique code after multiple attempts.", exception.Message);
            }
        }
    }

}