namespace Ostj.Shared.Contracts
{
    public record PersonProfile
    {
        public int Id { get; set; }
        public string? Name { get; set; }
        public string? Email { get; set; }
        public string[] JobTitles { get; set; } = [];
    }

}