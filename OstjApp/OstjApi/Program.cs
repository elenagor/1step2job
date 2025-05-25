using Microsoft.EntityFrameworkCore;
using OstjApi.Data;
using OstjApi.Services;

var builder = WebApplication.CreateBuilder(args);

// Register DbContext and ResumeService
builder.Services.AddDbContext<OstjDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

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