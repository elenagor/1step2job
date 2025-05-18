using Microsoft.EntityFrameworkCore;
using OstjApi.Models;

namespace OstjApi.Data
{
    public class OstjDbContext : DbContext
    {
        public OstjDbContext(DbContextOptions<OstjDbContext> options) : base(options) { }

        public DbSet<Person> Persons { get; set; }
    }
}