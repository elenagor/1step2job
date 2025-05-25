using Microsoft.EntityFrameworkCore;

namespace OstjApi.Models
{
    [Index(nameof(Email), nameof(Code), IsUnique = true)]
    public class Otc
    {
        public int Id { get; set; }
        public required string Email { get; set; }
        public required string Code { get; set; }
        public required DateTime Expires { get; set; }
        public bool IsUsed { get; set; } = false;
    }
   
 }