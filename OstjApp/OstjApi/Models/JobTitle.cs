using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Pgvector.EntityFrameworkCore;

namespace OstjApi.Models
{
    public class JobTitle
    {
        public int Id { get; set; }
        [MaxLength(200)]
        public required string Title { get; set; }
        [Column(TypeName = "vector(4096)")]
        public Pgvector.Vector? Embedding { get; set; }
        public required bool IsUserDefined { get; set; } = false;
    }
}