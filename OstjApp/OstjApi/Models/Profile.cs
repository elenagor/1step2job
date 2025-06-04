using System.ComponentModel.DataAnnotations;

namespace OstjApi.Models
{
    public class Profile
    {
        public int Id { get; set; }
        [MaxLength(100)]
        public required int PersonId { get; set; } = 0;
        public required string Name { get; set; }
#pragma warning disable CS8618 // Non-nullable field must contain a non-null value when exiting constructor. Consider adding the 'required' modifier or declaring as nullable.
        public ProfileDetails ProfileDetails { get; set; }
#pragma warning restore CS8618 // Non-nullable field must contain a non-null value when exiting constructor. Consider adding the 'required' modifier or declaring as nullable.
    }

    public class ProfileDetails
    {
        public int Id { get; set; }
        [MaxLength(100)]
        public required int PersonId { get; set; } = 0;
        public required string Name { get; set; }
        public List<JobTitle> JobTitles { get; set; } = [];
        public bool AcceptRemote { get; set; } = true;
        [MaxLength(200)]
        public string? Location { get; set; }
        public float? SalaryMin { get; set; }
        public float? SalaryMax { get; set; }
        public string? ExtraRequirements { get; set; }
        public required string Resume { get; set; }
    }
}