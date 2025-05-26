using System.ComponentModel.DataAnnotations.Schema;

namespace OstjApi.Models
{
    public class Profile
    {
        public int Id { get; set; }
        public string Title { get; set; } = string.Empty;
        public required int PersonId { get; set; }
        public required string Resume { get; set; }
    }
}