using System.ClientModel;
using System.Text.Json;
using OpenAI;
using OpenAI.Chat;

var promptPath = Path.Combine(AppContext.BaseDirectory, "../../..", "Resources", "ExtractPersonInfoPrompt.txt");
string promptTemplate = await File.ReadAllTextAsync(promptPath);
var resumePath = Path.Combine(AppContext.BaseDirectory, "../../..", "Resources", "Person.txt");
string resume = await File.ReadAllTextAsync(resumePath);

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

public class Person
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Phone { get; set; } = string.Empty;
    public string City { get; set; } = string.Empty;
    public string State { get; set; } = string.Empty;
    public ICollection<Resume> Resumes { get; set; } = new List<Resume>();
}

public class Resume
{
    public int Id { get; set; }
    public required int PersonId { get; set; }
    public required string Content { get; set; }
}