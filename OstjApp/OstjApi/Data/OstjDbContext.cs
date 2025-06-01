using Microsoft.EntityFrameworkCore;
using Pgvector.EntityFrameworkCore;
using OstjApi.Models;
using Microsoft.EntityFrameworkCore.ChangeTracking;
using OstjLib.Contracts;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;

namespace OstjApi.Data
{
    public class OstjDbContext : DbContext
    {
        public OstjDbContext() : base() { }
        public OstjDbContext(DbContextOptions<OstjDbContext> options) : base(options) { }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.HasPostgresExtension("vector");
            modelBuilder.Entity<Person>()
                .Property(p => p.EnrollmentType)
                .HasConversion<string>();
        }

        public virtual DbSet<Profile> Profiles { get; set; }

        public virtual DbSet<Person> Persons { get; set; }

        public virtual DbSet<Job> Jobs { get; set; }

        public virtual DbSet<Result> Results { get; set; }

        public virtual DbSet<Otc> Otcs { get; set; }

        public virtual DbSet<JobTitle> JobTitles { get; set; }
    }
}