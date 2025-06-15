using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Ostj.Constants;

namespace OstjApi.Models
{
    public class Location
    {
        [MaxLength(Constants.MaxNameLength)]
        public string? Country { get; set; }

        [MaxLength(Constants.MaxNameLength)]
        public string? StateOrRegion { get; set; }

        [MaxLength(Constants.MaxNameLength)]
        public string? City { get; set; }
    }
}