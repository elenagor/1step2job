using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace OstjApi.Models
{
    public class JobTitle
    {
        public int Id { get; set; }
        public int ProfileId { get; set; } = 0;
        [MaxLength(200)]
        public required string Title { get; set; }
#pragma warning disable CS8618 // Non-nullable field must contain a non-null value when exiting constructor. Consider adding the 'required' modifier or declaring as nullable.
        public JobTitleDetails JobTitleDetails { get; set; }
#pragma warning restore CS8618 // Non-nullable field must contain a non-null value when exiting constructor. Consider adding the 'required' modifier or declaring as nullable.
    }

    public class JobTitleDetails
    {
        public int Id { get; set; }
        public int ProfileId { get; set; } = 0;
        [MaxLength(200)]
        public required string Title { get; set; }
        [Column(TypeName = "vector(4096)")]
        public Pgvector.Vector? Embedding { get; set; }
        public required bool IsUserDefined { get; set; } = false;
    }
}