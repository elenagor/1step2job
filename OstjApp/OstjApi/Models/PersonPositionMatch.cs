using System.ComponentModel.DataAnnotations.Schema;

namespace OstjApi.Models
{
    public class PersonPositionMatch
    {
        public int Id { get; set; }
        public int PersonId { get; set; }
        [ForeignKey(nameof(PersonId))]
        public required Person Person { get; set; }
        public int ProfileId { get; set; }
        [ForeignKey(nameof(ProfileId))]
        public required Profile Profile { get; set; }
        public int PositionId { get; set; }
        [ForeignKey(nameof(PositionId))]
        public required Position Position { get; set; }
        public required int Score { get; set; }
        [Column(TypeName = "nvarchar(20)")]
        public required Status Status { get; set; } // Enum field
        public required DateTime Date { get; set; }
        public String? Reasoning { get; set; }
        [Column(TypeName = "json")]
        public ComparisonDetail[] ComparisonDetails { get; set; } = [];
    }

    public class ComparisonDetail
    {
        public float Score { get; set; }
        public string? Reasoning { get; set; }
    }
    public enum Status
    {
        Started,
        Finished
    }
}