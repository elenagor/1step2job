using Pgvector.EntityFrameworkCore;
using System.Reflection.Emit;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations.Schema;


namespace OstjApi.Models
{
    public class Job
    {
        public int Id { get; set; }
        public required string ExternalId { get; set; }
        public required string Title { get; set; }
        [Column(TypeName = "vector(4096)")]
        public Pgvector.Vector? TitleEmbeddings { get; set; }
        public string? LocationCountry { get; set; }
        public string? LocationCity { get; set; }
        public string? LocationState { get; set; }
        public bool? LocationIsRemote { get; set; }
        public DateTime Published { get; set; }
        public required string Description { get; set; }
        public required string ApplyUrl { get; set; }
        public float? SalaryMin { get; set; }
        public float? SalaryMax { get; set; }
        public string? Type { get; set; }
    }
}