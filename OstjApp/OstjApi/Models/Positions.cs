using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;
using Ostj.Constants;


namespace OstjApi.Models
{
    public class Position
    {
        public int Id { get; set; }
        [MaxLength(Constants.MaxNameLength)]
        public required string ExternalId { get; set; }
        [MaxLength(Constants.MaxTitleLength)]
        public required string Title { get; set; }
        [Column(TypeName = "vector(4096)")]
        public Pgvector.Vector? TitleEmbeddings { get; set; }
        [MaxLength(Constants.MaxNameLength)]
        public string? LocationCountry { get; set; }
        [MaxLength(Constants.MaxNameLength)]
        public Location? Location { get; set; }
        public bool? IsRemote { get; set; }
        public DateTime Published { get; set; }
        public required string Description { get; set; }
        [MaxLength(500)]
        public required string ApplyUrl { get; set; }
        public float? SalaryMin { get; set; }
        public float? SalaryMax { get; set; }
        [MaxLength(Constants.MaxNameLength)]
        public string? Type { get; set; }
    }
}