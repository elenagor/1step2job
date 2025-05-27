using Microsoft.EntityFrameworkCore;

namespace OstjApi.Models
{
    [Index(nameof(ext_id), IsUnique = false)]
    [Index(nameof(title), IsUnique = false)]
    [Index(nameof(location), IsUnique = false)]
    [Index(nameof(published), IsUnique = false)]
    [Index(nameof(remote), IsUnique = false)]
    [Index(nameof(type), IsUnique = false)]
    public class Job
    {
        [Key]
        public int Id;
        public String ext_id;
        public String title;
        public String location;
        public String published;
        public String description;
        public String application_url;
        public String salary;
        public String remote;
        public String type;
    }
}