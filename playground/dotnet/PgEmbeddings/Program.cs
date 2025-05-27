// See https://aka.ms/new-console-template for more information
using System.ClientModel;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json;
using Microsoft.EntityFrameworkCore;
using OpenAI;
using OpenAI.Chat;
using OpenAI.Embeddings;



//using Pgvector;
using Pgvector.EntityFrameworkCore;

namespace Ostj.Test;

public class Program
{
    public static void Main(string[] args)
    {
        using var db = new JobTitleContext();
        db.Database.EnsureCreated();
        var promptPath = Path.Combine(AppContext.BaseDirectory, "../../..", "Resources", "ExtractPersonInfoPrompt.txt");
        string promptTemplate = File.ReadAllText(promptPath);
        var resumePath = Path.Combine(AppContext.BaseDirectory, "../../..", "Resources", "Person.txt");
        string resume = File.ReadAllText(resumePath);

        var prompt = promptTemplate.Replace("{resume}", resume);


        OpenAIClientOptions options = new OpenAIClientOptions()
        {
            Endpoint = new Uri("http://localhost:8000/v1")
        };
        ChatClient client = new("qwen", new ApiKeyCredential("EMPTY"), options);
        ChatCompletion completion = client.CompleteChat(prompt);


        Console.WriteLine(completion.Content[0].Text);
        string responseText = completion.Content[0].Text.Trim();
        int startIdx = responseText.IndexOf('{');
        int endIdx = responseText.LastIndexOf('}');
        if (startIdx == -1 || endIdx == -1 || endIdx <= startIdx)
            throw new InvalidOperationException("OpenAI response does not contain valid JSON.");

        string json = responseText.Substring(startIdx, endIdx - startIdx + 1);

        var person = JsonSerializer.Deserialize<Person>(json);
        Console.WriteLine($"Name: {person?.Name}");
        Console.WriteLine($"Email: {person?.Email}");
        Console.WriteLine($"Phone: {person?.Phone}");
        Console.WriteLine($"JobTitles: {person?.JobTitles}");

        EmbeddingClient embeddingClient = new("qwen", new ApiKeyCredential("EMPTY"), options);
        foreach (var position in person?.JobTitles ?? [])
        {
            var embedding = embeddingClient.GenerateEmbedding(position).Value.ToFloats().ToArray();
            var jobTitle = new JobTitle
            {
                Title = position,
                Embedding = new Pgvector.Vector(embedding)
            };
            db.JobTitles.Add(jobTitle);
            Console.WriteLine($"Position: {position}, Embedding: [{string.Join(", ", embedding.Take(5))}...]");
        }
        db.SaveChanges();

        CalculateCosignDistance(db, embeddingClient, "Director of Operations");
        CalculateCosignDistance(db, embeddingClient, "Software Engineer");
        CalculateCosignDistance(db, embeddingClient, "Nurse");
        CalculateCosignDistance(db, embeddingClient, "Data Scientist");
        CalculateCosignDistance(db, embeddingClient, "Product Manager");
        CalculateCosignDistance(db, embeddingClient, "Chief Executive Officer");

        Console.WriteLine("Done.");
    }

    private static void CalculateCosignDistance(JobTitleContext db, EmbeddingClient embeddingClient, string pivotTitle)
    {
        var pivotEmbedding = embeddingClient.GenerateEmbedding(pivotTitle).Value.ToFloats().ToArray();
        Console.WriteLine($"Pivot JobTitile: {pivotTitle}, Embedding: [{string.Join(", ", pivotEmbedding.Take(5))}...]");
        var items = db.JobTitles
            .Select(x => new
            {
                x.Id,
                x.Title,
                CosineDistance = x.Embedding!.CosineDistance(new Pgvector.Vector(pivotEmbedding))
            })
            .ToList();
        foreach (var item in items)
        {
            Console.WriteLine($"Id: {item.Id}, Title: {item.Title}, CosineSimilarity: {1 - item.CosineDistance}");
        }
    }
}

public class Person
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Phone { get; set; } = string.Empty;
    public string City { get; set; } = string.Empty;
    public string State { get; set; } = string.Empty;
    public IList<string> JobTitles { get; set; } = [];
}

public class JobTitle
{
    public int Id { get; set; }
    public string Title { get; set; } = string.Empty;

    [Column(TypeName = "vector(4096)")]
    public Pgvector.Vector? Embedding { get; set; }
}

public class JobTitleContext : DbContext
{
    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
    {
        optionsBuilder
            .UseNpgsql("Host=localhost;Database=ostjdb;Username=ostjsvc;Password=ostjsvc!", option => option.UseVector())
            .UseSnakeCaseNamingConvention();
    }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.HasPostgresExtension("vector");
    }

    public DbSet<JobTitle> JobTitles { get; set; }
}