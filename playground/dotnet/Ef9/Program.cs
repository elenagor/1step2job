using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json;
using System.Text.Json.Nodes;
using Microsoft.EntityFrameworkCore;


internal class Program
{
    private static void Main(string[] args)
    {
        using (var db = new MyContext())
        {
            db.Database.EnsureCreated();

            if (db.Persons.Count() == 0)
            {
                Console.WriteLine("Seeded...");
                var person = new Person
                {
                    Name = "John Doe",
                    Email = "",
                    EnrollmentType = EnrollmentType.Basic,
                    Profiles =
                    [
                        new() {
                    Name = "Software Engineer",
                    PersonId = 0,
                    ProfileDetails = new ProfileDetails
                    {
                        Name = "Software Engineer",
                        AcceptRemote = true,
                        Location = "New York",
                        SalaryMin = 60000,
                        SalaryMax = 120000,
                        PersonId = 0,
                        ExtraRequirements = "5+ years of experience in software development.",
                        Resume = "Resume content here.",
                        JobTitles =
                        [
                            new() {
                                Title = "Senior Software Engineer",
                                ProfileId = 0,
                                JobTitleDetails = new JobTitleDetails
                                {
                                    Title = "Senior Software Engineer",
                                    IsUserDefined = true,
                                    ProfileId = 0,
                                }
                            }
                        ]
                    }
                }
                    ]
                };

                db.Persons.Add(person);
                db.SaveChanges();
            }
        }

        using (var db = new MyContext())
        {
            var person1 = db.Persons
                 .Include(p => p.Profiles)
                 .FirstOrDefault(p => p.Id == 1);
            Console.WriteLine(JsonSerializer.Serialize(person1));
        }

        using (var db = new MyContext())
        {
            var person1 = db.Persons
                 .Include(p => p.Profiles)
                 .ThenInclude(p => p.ProfileDetails)
                 .ThenInclude(p => p.JobTitles)
                 .FirstOrDefault(p => p.Id == 1);
            Console.WriteLine(JsonSerializer.Serialize(person1));
        }
    }

    public class MyContext : DbContext
    {
        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            optionsBuilder
                .UseNpgsql("Host=localhost;Database=ostjdb_test;Username=ostjsvc;Password=ostjsvc!", option => option.UseVector())
                .UseSnakeCaseNamingConvention();
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
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
            #endregion

        }

        public DbSet<Person> Persons { get; set; }
        public DbSet<ProfileDetails> Profiles { get; set; }
        public DbSet<JobTitleDetails> JobTitles { get; set; }
    }

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

    public class Profile
    {
        public int Id { get; set; }
        [MaxLength(100)]
        public int PersonId { get; set; } = 0;
        public required string Name { get; set; }
#pragma warning disable CS8618 // Non-nullable field must contain a non-null value when exiting constructor. Consider adding the 'required' modifier or declaring as nullable.
        public ProfileDetails ProfileDetails { get; set; }
#pragma warning restore CS8618 // Non-nullable field must contain a non-null value when exiting constructor. Consider adding the 'required' modifier or declaring as nullable.
    }

    public class ProfileDetails
    {
        public int Id { get; set; }
        [MaxLength(100)]
        public int PersonId { get; set; } = 0;
        public required string Name { get; set; }
        public List<JobTitle> JobTitles { get; set; } = [];
        public bool AcceptRemote { get; set; } = true;
        [MaxLength(200)]
        public string? Location { get; set; }
        public float? SalaryMin { get; set; }
        public float? SalaryMax { get; set; }
        public string? ExtraRequirements { get; set; }
        public required string Resume { get; set; }
    }

    public class JobTitle
    {
        public int Id { get; set; }
        public int ProfileId { get; set; } = 0;
        [MaxLength(200)]
        public required string Title { get; set; }
#pragma warning disable CS8618 // Non-nullable field must contain a non-null value when exiting constructor. Consider adding the 'required' modifier or declaring as nullable.
        public JobTitleDetails JobTitleDetails { get; set; }
#pragma warning restore CS8618 // Non-nullable field must contain a non-null value when exiting constructor. Consider adding the 'required' modifier or declaring as nullable.
    }

    public class JobTitleDetails
    {
        public int Id { get; set; }
        public int ProfileId { get; set; } = 0;
        [MaxLength(200)]
        public required string Title { get; set; }
        [Column(TypeName = "vector(4096)")]
        public Pgvector.Vector? Embedding { get; set; }
        public required bool IsUserDefined { get; set; } = false;
    }


    public enum EnrollmentType
    {
        NotEnrolled,
        Basic,
        Premium
    }


}