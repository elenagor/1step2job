using System.ComponentModel.DataAnnotations;
using Ostj.Constants;

namespace OstjApi.Models
{
    public class Profile
    {
        public int Id { get; set; }
        public required int PersonId { get; set; } = 0;
        [MaxLength(Constants.MaxNameLength)]
        public required string Name { get; set; }
#pragma warning disable CS8618 // Non-nullable field must contain a non-null value when exiting constructor. Consider adding the 'required' modifier or declaring as nullable.
        public ProfileDetails ProfileDetails { get; set; }
#pragma warning restore CS8618 // Non-nullable field must contain a non-null value when exiting constructor. Consider adding the 'required' modifier or declaring as nullable.
    }

    public class ProfileDetails
    {
        public int Id { get; set; }
        public required int PersonId { get; set; } = 0;
        [MaxLength(Constants.MaxNameLength)]
        public required string Name { get; set; }
        public List<JobTitle> JobTitles { get; set; } = [];
        public bool AcceptRemote { get; set; } = true;
        [MaxLength(Constants.MaxNameLength)]
        public Location Location { get; set; } = new();
        public float? SalaryMin { get; set; }
        public float? SalaryMax { get; set; }
        public string? ExtraRequirements { get; set; }
        public required string Resume { get; set; }
    }
}