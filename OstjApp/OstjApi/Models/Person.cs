using System.ComponentModel.DataAnnotations;
using Microsoft.EntityFrameworkCore;
using Ostj.Constants;

namespace OstjApi.Models
{
    [Index(nameof(Email), IsUnique = true)]
    public class Person
    {
        public int Id { get; set; }
        [MaxLength(Constants.MaxTitleLength)]
        public string Name { get; set; } = string.Empty;
        [MaxLength(Constants.MaxNameLength)]
        public string Email { get; set; } = string.Empty;
        [MaxLength(20)]
        public string? Phone { get; set; }
        [MaxLength(Constants.MaxNameLength)]
        public EnrollmentType EnrollmentType { get; set; } = EnrollmentType.NotEnrolled;
        public IList<Profile> Profiles { get; set; } = [];
    }
}