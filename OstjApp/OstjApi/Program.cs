using Microsoft.EntityFrameworkCore;
using OstjApi.Data;
using OstjApi.Services;

var builder = WebApplication.CreateBuilder(args);

// Register DbContext and ResumeService
builder.Services.AddDbContext<OstjDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

builder.Services.AddSingleton<IAIClient>(new AIClient(
    builder.Configuration["OpenAI:Model"] ?? throw new InvalidOperationException("OpenAI:Model configuration is missing."),
    builder.Configuration["OpenAI:ApiUri"] ?? throw new InvalidOperationException("OpenAI:ApiUri configuration is missing."),
    builder.Configuration["OpenAI:ApiKey"] ?? throw new InvalidOperationException("OpenAI:ApiKey configuration is missing.")));

builder.Services
    .AddScoped<IPersonService, PersonService>()
    .AddScoped<IAuthService, AuthService>()
    .AddScoped<IEmailService>(provider =>
    {
        var config = provider.GetRequiredService<IConfiguration>();
        return new EmailService(
            config["Email:SmtpServer"] ?? throw new InvalidOperationException("Email:Host configuration is missing."),
            config.GetValue<int?>("Email:Port") ?? throw new InvalidOperationException("Email:Port configuration is missing."),
            config["Email:Username"] ?? throw new InvalidOperationException("Email:Username configuration is missing."),
            config["Email:Password"] ?? throw new InvalidOperationException("Email:Password configuration is missing."),
            config.GetValue<bool?>("Email:Ssl") ?? throw new InvalidOperationException("Email:Ssl configuration is missing.")
        );
    });

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();

// Minimal API endpoint for uploading resume
app.MapPost("/api/resume/upload", async (HttpRequest request, IPersonService personService) =>
{
    if (!request.HasFormContentType)
        return Results.BadRequest("No form data.");

    var form = await request.ReadFormAsync();
    var file = form.Files["file"];
    if (file == null || file.Length == 0)
        return Results.BadRequest("No file uploaded.");

    using var ms = new MemoryStream();
    await file.CopyToAsync(ms);

    var person = await personService.SavePersonAsync(
        file.FileName,
        file.ContentType,
        ms.ToArray()
    );

    return Results.Ok(new { person.Id, person.Name, person.Email });
});

app.MapPost("/api/auth/sendotc", async (HttpRequest request, IAuthService authService, IEmailService emailService) =>
{
    if (!request.HasFormContentType)
        return Results.BadRequest("No form data.");

    var form = await request.ReadFormAsync();
    var email = form["email"].ToString();
    if (string.IsNullOrEmpty(email))
        return Results.BadRequest("Email is required.");
        
    var code = await authService.GenerateCodeAsync(email);
    await emailService.SendOtcEmailAsync(email, code);
    return Results.Ok();
});

app.Run();