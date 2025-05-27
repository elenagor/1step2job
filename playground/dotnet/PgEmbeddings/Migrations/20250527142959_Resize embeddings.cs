using Microsoft.EntityFrameworkCore.Migrations;
using Pgvector;

#nullable disable

namespace PgEmbeddings.Migrations
{
    /// <inheritdoc />
    public partial class Resizeembeddings : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<Vector>(
                name: "embedding",
                table: "job_titles",
                type: "vector(4096)",
                nullable: true,
                oldClrType: typeof(Vector),
                oldType: "vector(1536)",
                oldNullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<Vector>(
                name: "embedding",
                table: "job_titles",
                type: "vector(1536)",
                nullable: true,
                oldClrType: typeof(Vector),
                oldType: "vector(4096)",
                oldNullable: true);
        }
    }
}
