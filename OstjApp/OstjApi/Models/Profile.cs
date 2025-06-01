using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace OstjApi.Models
{
    public class Profile
    {
        public int Id { get; set; }
        public List<JobTitle> JobTitles { get; set; } = [];
        public required int PersonId { get; set; }
        public bool AcceptRemote { get; set; } = true;
        [MaxLength(200)]
        public string? Location { get; set; }
        public float? SalaryMin { get; set; }
        public float? SalaryMax { get; set; }
        public required string Resume { get; set; }
    }
}