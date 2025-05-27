using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace OstjApi.Models
{
    public class Result
    {
        public int Id  { get; set; }
        public int PersonId { get; set; }
        [ForeignKey(nameof(PersonId))]
        public required Person Person { get; set; }
        public int ProfileId { get; set; }
        [ForeignKey(nameof(ProfileId))]
        public required Profile Profile { get; set; }
        public int JobId { get; set; }
        [ForeignKey(nameof(JobId))]
        public required Job Job { get; set; }
        public required int Score { get; set; }
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
}