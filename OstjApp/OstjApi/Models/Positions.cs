using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;


namespace OstjApi.Models
{
    public class Position
    {
        public int Id { get; set; }
        [MaxLength(100)]
        public required string ExternalId { get; set; }
        [MaxLength(200)]
        public required string Title { get; set; }
        [Column(TypeName = "vector(4096)")]
        public Pgvector.Vector? TitleEmbeddings { get; set; }
        [MaxLength(100)]
        public string? LocationCountry { get; set; }
        [MaxLength(100)]
        public string? LocationCity { get; set; }
        [MaxLength(2)]
        public string? LocationState { get; set; }
        public bool? LocationIsRemote { get; set; }
        public DateTime Published { get; set; }
        public required string Description { get; set; }
        [MaxLength(500)]
        public required string ApplyUrl { get; set; }
        public float? SalaryMin { get; set; }
        public float? SalaryMax { get; set; }
        [MaxLength(100)]
        public string? Type { get; set; }
    }
}