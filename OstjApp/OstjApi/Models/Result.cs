using Microsoft.EntityFrameworkCore;

namespace OstjApi.Models
{
    public class Result
    {
        [Key]
        public int Id;
        [ForeignKey("PersonId")]
        public int PersonId;
        [ForeignKey("ProfileId")]
        public int ProfileId;
        [ForeignKey("JobId")]
        public int JobId;
        public int overall_score;
        public Date date;
        public String Reasoning;
        public Json key_arias_of_comparison;
    }
}