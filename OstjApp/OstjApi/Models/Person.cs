using System.ComponentModel.DataAnnotations;
using Microsoft.EntityFrameworkCore;
using Ostj.Constants;

namespace OstjApi.Models
{
    [Index(nameof(Email), IsUnique = true)]
    public class Person
    {
        public int Id { get; set; }
        [MaxLength(200)]
        public string Name { get; set; } = string.Empty;
        [MaxLength(200)]
        public string Email { get; set; } = string.Empty;
        [MaxLength(20)]
        public string? Phone { get; set; }
        [MaxLength(100)]
        public string? City { get; set; }
        [MaxLength(2)]
        public string? State { get; set; }
        public EnrollmentType EnrollmentType { get; set; } = EnrollmentType.NotEnrolled;
        public IList<Profile> Profiles { get; set; } = [];
    }
}