using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Ostj.Shared.Contracts;
using OstjApi.Data;
using OstjApi.Services;

var builder = WebApplication.CreateBuilder(args);

// Register DbContext and ResumeService
builder.Services.AddDbContext<OstjDbContext>(options =>
    options
        .UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection"), option => option.UseVector())
        .UseSnakeCaseNamingConvention());

//builder.Services.AddAntiforgery();

builder.Services
    .Configure<AIClientSettings>(builder.Configuration.GetSection("OpenAI"))
    .Configure<EmailSettings>(builder.Configuration.GetSection("Email"))
    .Configure<OtcSettings>(builder.Configuration.GetSection("Otc"));


builder.Services.AddSingleton<IAIClient, AIClient>();

builder.Services
    .AddScoped<IPersonService, PersonService>()
    .AddScoped<IAuthService, AuthService>()
    .AddScoped<IEmailService, EmailService>();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();
//app.UseAntiforgery();

app.MapGet("/api/person/{id:int}", async ([FromRoute] int id, IPersonService personService) =>
{
    if (id <= 0)
        return Results.BadRequest("Invalid person ID.");
    try
    {
        var person = await personService.GetPersonAsync(id);
        if (person == null)
            return Results.NotFound();

        return Results.Ok(new PersonInfo
        {
            Id = person.Id,
            Name = person.Name,
            Email = person.Email,
            Phone = person.Phone,
            Profiles = [.. person.Profiles
                .Select(p => new ProfileInfo
                {
                    Id = p.Id,
                    Name = p.Name
                })]
        });
    }
    catch (Exception ex)
    {
        return Results.Problem(ex.Message);
    }

})
.DisableAntiforgery();

app.MapGet("/api/person/{id:int}/profile/{pid:int}", async ([FromRoute] int id, [FromRoute] int pid, IPersonService personService) =>
{
    if (id <= 0)
        return Results.BadRequest("Invalid person ID.");
    if (pid <= 0)
        return Results.BadRequest("Invalid profile ID.");
        
    try
    {
        var profile = await personService.GetProfileDetailsAsync(id, pid);
        if (profile == null)
            return Results.NotFound("Profile not found for the given person ID.");

        return Results.Ok(new PersonProfile
        {
            Id = profile.Id,
            Name = profile.Name,
            JobTitles = [.. profile.JobTitles
                .Take(3)
                .Select(jt => jt.Title)],
            AcceptRemote = profile.AcceptRemote,
            Location = profile.Location,
            SalaryMin = profile.SalaryMin,
            SalaryMax = profile.SalaryMax,
            ExtraRequirements = profile.ExtraRequirements
        });
    }
    catch (Exception ex)
    {
        return Results.Problem(ex.Message);
    }
})
.DisableAntiforgery();

// Minimal API endpoint for uploading resume
app.MapPost("/api/resume/upload/{id}", async ([FromRoute] int id, IFormFile file, IPersonService personService) =>
{
    if (file == null || file.Length == 0)
        return Results.BadRequest("No file uploaded.");

    using var ms = new MemoryStream();
    await file.CopyToAsync(ms);

    try
    {
        var profileId = await personService.SaveProfileFromResumeAsync(
            id,
            file.FileName,
            file.ContentType,
            ms.ToArray()
        );

        return Results.Ok(profileId);
    }
    catch (Exception ex)
    {
        return Results.Problem(ex.Message);
    }

})
.DisableAntiforgery();

app.MapPost("/api/auth/sendotc", async ([FromForm] string email, IAuthService authService, IEmailService emailService) =>
{
    if (string.IsNullOrEmpty(email))
        return Results.BadRequest("Email is required.");

    var code = await authService.GenerateCodeAsync(email);
    try
    {
        await emailService.SendOtcEmailAsync(email, code);
    }
    catch (Exception ex)
    {
        app.Logger.LogError(ex, "Failed to send OTC email to {Email}", email);
        return Results.Problem("Failed to send email: " + ex.Message);
    }
    return Results.Ok();
})
.DisableAntiforgery();

app.MapPost("/api/auth/login", async ([FromForm] string email, [FromForm] string code, IAuthService authService) =>
{
    if (string.IsNullOrEmpty(email) || string.IsNullOrEmpty(code))
        return Results.BadRequest("Email and code are required.");

    var authResult = await authService.ValidateCodeAsync(email, code);
    if (authResult.User.UserId < 0)
        return Results.Unauthorized();

    // Optionally, generate and return a JWT or session token here
    return Results.Ok(authResult.User);
})
.DisableAntiforgery();

app.Run();