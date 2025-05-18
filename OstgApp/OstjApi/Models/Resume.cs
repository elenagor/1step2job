using System.ComponentModel.DataAnnotations.Schema;

namespace OstjApi.Models
{
    [Table("Resumes")]
    public class Resume
    {
        public int Id { get; set; }
        public required int PersonId { get; set; }
        public required string Content { get; set; }
    }
}