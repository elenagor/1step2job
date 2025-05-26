using Microsoft.EntityFrameworkCore;

namespace OstjApi.Models
{
    [Index(nameof(Email), IsUnique = true)]
    public class Person
    {
        public int Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string? Phone { get; set; }
        public string? City { get; set; }
        public string? State { get; set; }
        public IList<Profile> Profiles { get; set; } = [];
    }
}