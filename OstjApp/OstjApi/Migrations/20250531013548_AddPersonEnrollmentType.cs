using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace OstjApi.Migrations
{
    /// <inheritdoc />
    public partial class AddPersonEnrollmentType : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "enrollment_type",
                table: "persons",
                type: "text",
                nullable: false,
                defaultValue: "");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "enrollment_type",
                table: "persons");
        }
    }
}
