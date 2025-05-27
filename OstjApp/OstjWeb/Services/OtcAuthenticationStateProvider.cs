using Microsoft.AspNetCore.Components.Authorization;
using Microsoft.AspNetCore.Identity;
using System.Security.Claims;
using System.Threading.Tasks;

namespace OstjWeb.Services;

public class OtcAuthenticationStateProvider : AuthenticationStateProvider
{
    private bool _isAuthenticated = false;
    private string? _email;

    public void MarkUserAsAuthenticated(string email)
    {
        _isAuthenticated = true;
        _email = email;
        NotifyAuthenticationStateChanged(GetAuthenticationStateAsync());
    }

    public void MarkUserAsLoggedOut()
    {
        _isAuthenticated = false;
        _email = null;
        NotifyAuthenticationStateChanged(GetAuthenticationStateAsync());
    }

    public override Task<AuthenticationState> GetAuthenticationStateAsync()
    {
        ClaimsIdentity identity = _isAuthenticated && _email != null
            ? new ClaimsIdentity(new[] { new Claim(ClaimTypes.Name, _email) }, "OtcAuth")
            : new ClaimsIdentity();

        return Task.FromResult(new AuthenticationState(new ClaimsPrincipal(identity)));
    }
}