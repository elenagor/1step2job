namespace Ostj.Shared.Contracts
{
    public record PersonInfo
    {
        public int Id { get; set; }
        public string? Name { get; set; }
        public string? Email { get; set; }
        public string? Phone { get; set; }
        public ProfileInfo[] Profiles { get; set; } = [];
    }

    public record ProfileInfo
    {
        public int Id { get; set; }
        public required string Name { get; set; }
    }

    public record PersonProfile
    {
        public int Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public string[] JobTitles { get; set; } = [];
        public bool AcceptRemote { get; set; } = true;
        public LocationInfo? Location { get; set; }
        public float? SalaryMin { get; set; }
        public float? SalaryMax { get; set; }
        public string? ExtraRequirements { get; set; }
    }

    public class PositionInfo
    {
        public int Id { get; set; }
        public required string ExternalId { get; set; }
        public required string Title { get; set; }
        public LocationInfo? Location { get; set; }
        public bool? IsRemote { get; set; }
        public DateTime Published { get; set; }
        public required string Description { get; set; }
        public required string ApplyUrl { get; set; }
        public float? SalaryMin { get; set; }
        public float? SalaryMax { get; set; }
    }

    public class LocationInfo
    {
        public string? Country { get; set; }
        public string? StateOrRegion { get; set; }
        public string? City { get; set; }
    }

}