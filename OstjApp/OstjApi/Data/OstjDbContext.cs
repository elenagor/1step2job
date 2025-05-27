using Microsoft.EntityFrameworkCore;
using Pgvector.EntityFrameworkCore;
using OstjApi.Models;

namespace OstjApi.Data
{
    public class OstjDbContext : DbContext
    {
        public OstjDbContext() : base() { }
        public OstjDbContext(DbContextOptions<OstjDbContext> options) : base(options) { }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.HasPostgresExtension("vector");
        }

        public virtual DbSet<Profile> Profiles { get; set; }

        public virtual DbSet<Person> Persons { get; set; }

        public virtual DbSet<Job> Jobs { get; set; }

        public virtual DbSet<Result> Results { get; set; }

        public virtual DbSet<Otc> Otcs { get; set; }

        public virtual DbSet<JobTitle> JobTitles { get; set; }
    }
}