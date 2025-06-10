using Microsoft.EntityFrameworkCore;
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
            modelBuilder.Entity<Person>()
                .Property(p => p.EnrollmentType)
                .HasConversion<string>();
            modelBuilder.HasPostgresExtension("vector");

            #region TableSplitting
            modelBuilder.Entity<ProfileDetails>(
                ob =>
                {
                    ob.ToTable("profiles");
                    ob.Property(o => o.Name).HasColumnName("name");
                    ob.Property(o => o.PersonId).HasColumnName("person_id");
                    ob.HasMany(o => o.JobTitles)
                        .WithOne()
                        .HasForeignKey(o => o.ProfileId)
                        .OnDelete(DeleteBehavior.Cascade);
                });

            modelBuilder.Entity<Profile>(
                ob =>
                {
                    ob.ToTable("profiles");
                    ob.Property(o => o.Name).HasColumnName("name");
                    ob.Property(o => o.PersonId).HasColumnName("person_id");
                    ob.HasOne(o => o.ProfileDetails).WithOne()
                        .HasForeignKey<ProfileDetails>(o => o.Id);
                    ob.Navigation(o => o.ProfileDetails).IsRequired();
                });

            modelBuilder.Entity<JobTitleDetails>(
                ob =>
                {
                    ob.ToTable("job_titles");
                    ob.Property(o => o.Title).HasColumnName("title");
                    ob.Property(o => o.ProfileId).HasColumnName("profile_id");
                });

            modelBuilder.Entity<JobTitle>(
                ob =>
                {
                    ob.ToTable("job_titles");
                    ob.Property(o => o.Title).HasColumnName("title");
                    ob.Property(o => o.ProfileId).HasColumnName("profile_id");
                    ob.HasOne(o => o.JobTitleDetails).WithOne()
                        .HasForeignKey<JobTitleDetails>(o => o.Id);
                    ob.Navigation(o => o.JobTitleDetails).IsRequired();
                });

            modelBuilder.Entity<PersonPositionMatch>()
                .Property(p => p.Score)
                .HasDefaultValue(-1);
            #endregion

        }

        public virtual DbSet<Person> Persons { get; set; }
        public virtual DbSet<ProfileDetails> Profiles { get; set; }
        public virtual DbSet<JobTitleDetails> JobTitles { get; set; }
        public virtual DbSet<Position> Positions { get; set; }
        public virtual DbSet<PersonPositionMatch> PersonPositionMatches { get; set; }
        public virtual DbSet<Otc> Otcs { get; set; }

    }
}