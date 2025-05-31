using System.Text.Json;
using System.Net.Http.Formatting;
using Ostj.Shared.Contracts;

namespace OstjWeb.Services
{
    public class AuthService
    {
        private readonly HttpClient _httpClient;

        public AuthService(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }

        public async Task<AuthenticatedUser?> Login(string email, string code)
        {
            var formData = new List<KeyValuePair<string, string>>
            {
                new KeyValuePair<string, string>("email", email),
                new KeyValuePair<string, string>("code", code)
            };
            var content = new FormUrlEncodedContent(formData);
            var result = await _httpClient.PostAsync("auth/login", content);

            if (result.IsSuccessStatusCode)
            {
                var response = await result.Content.ReadAsAsync<AuthenticatedUser>();
                return response;
            }
            else
            {
                return null;
            }
        }

        public async Task<bool> SendOtc(string email)
        {
            var formData = new List<KeyValuePair<string, string>>
            {
                new KeyValuePair<string, string>("email", email)
            };
            var content = new FormUrlEncodedContent(formData);

            var result = await _httpClient.PostAsync("auth/sendotc", content);

            return result.IsSuccessStatusCode;
        }
    }

    public class AuthRequest
    {
        public string? Email { get; set; }
        public string? Code { get; set; }
    }

}