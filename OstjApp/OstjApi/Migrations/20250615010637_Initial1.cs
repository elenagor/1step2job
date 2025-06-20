﻿using System;
using Microsoft.EntityFrameworkCore.Migrations;
using Npgsql.EntityFrameworkCore.PostgreSQL.Metadata;
using OstjApi.Models;
using Pgvector;

#nullable disable

namespace OstjApi.Migrations
{
    /// <inheritdoc />
    public partial class Initial1 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterDatabase()
                .Annotation("Npgsql:PostgresExtension:vector", ",,");

            migrationBuilder.CreateTable(
                name: "otcs",
                columns: table => new
                {
                    id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    email = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    code = table.Column<string>(type: "character varying(12)", maxLength: 12, nullable: false),
                    expires = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    is_used = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_otcs", x => x.id);
                });

            migrationBuilder.CreateTable(
                name: "persons",
                columns: table => new
                {
                    id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    name = table.Column<string>(type: "character varying(200)", maxLength: 200, nullable: false),
                    email = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    phone = table.Column<string>(type: "character varying(20)", maxLength: 20, nullable: true),
                    enrollment_type = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_persons", x => x.id);
                });

            migrationBuilder.CreateTable(
                name: "positions",
                columns: table => new
                {
                    id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    external_id = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    title = table.Column<string>(type: "character varying(200)", maxLength: 200, nullable: false),
                    title_embeddings = table.Column<Vector>(type: "vector(4096)", nullable: true),
                    is_remote = table.Column<bool>(type: "boolean", nullable: true),
                    published = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    description = table.Column<string>(type: "text", nullable: false),
                    apply_url = table.Column<string>(type: "character varying(500)", maxLength: 500, nullable: false),
                    salary_min = table.Column<float>(type: "real", nullable: true),
                    salary_max = table.Column<float>(type: "real", nullable: true),
                    type = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: true),
                    location_city = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: true),
                    location_country = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: true),
                    location_state_or_region = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_positions", x => x.id);
                });

            migrationBuilder.CreateTable(
                name: "profiles",
                columns: table => new
                {
                    id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    person_id = table.Column<int>(type: "integer", nullable: false),
                    name = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    accept_remote = table.Column<bool>(type: "boolean", nullable: false),
                    salary_min = table.Column<float>(type: "real", nullable: true),
                    salary_max = table.Column<float>(type: "real", nullable: true),
                    extra_requirements = table.Column<string>(type: "text", nullable: true),
                    resume = table.Column<string>(type: "text", nullable: false),
                    location_city = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: true),
                    location_country = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: true),
                    location_state_or_region = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_profiles", x => x.id);
                    table.ForeignKey(
                        name: "fk_profiles_persons_person_id",
                        column: x => x.person_id,
                        principalTable: "persons",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "job_titles",
                columns: table => new
                {
                    id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    profile_id = table.Column<int>(type: "integer", nullable: false),
                    title = table.Column<string>(type: "character varying(200)", maxLength: 200, nullable: false),
                    embedding = table.Column<Vector>(type: "vector(4096)", nullable: true),
                    is_user_defined = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_job_titles", x => x.id);
                    table.ForeignKey(
                        name: "fk_job_titles_profiles_profile_id",
                        column: x => x.profile_id,
                        principalTable: "profiles",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "person_position_matches",
                columns: table => new
                {
                    id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    person_id = table.Column<int>(type: "integer", nullable: false),
                    profile_id = table.Column<int>(type: "integer", nullable: false),
                    position_id = table.Column<int>(type: "integer", nullable: false),
                    score = table.Column<int>(type: "integer", nullable: false, defaultValue: -1),
                    date = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    reasoning = table.Column<string>(type: "text", nullable: true),
                    is_sent = table.Column<bool>(type: "boolean", nullable: false),
                    comparison_details = table.Column<ComparisonDetail[]>(type: "json", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_person_position_matches", x => x.id);
                    table.ForeignKey(
                        name: "fk_person_position_matches_persons_person_id",
                        column: x => x.person_id,
                        principalTable: "persons",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_person_position_matches_positions_position_id",
                        column: x => x.position_id,
                        principalTable: "positions",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_person_position_matches_profile_profile_id",
                        column: x => x.profile_id,
                        principalTable: "profiles",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "ix_job_titles_profile_id",
                table: "job_titles",
                column: "profile_id");

            migrationBuilder.CreateIndex(
                name: "ix_otcs_email_code",
                table: "otcs",
                columns: new[] { "email", "code" },
                unique: true);

            migrationBuilder.CreateIndex(
                name: "ix_person_position_matches_person_id",
                table: "person_position_matches",
                column: "person_id");

            migrationBuilder.CreateIndex(
                name: "ix_person_position_matches_position_id",
                table: "person_position_matches",
                column: "position_id");

            migrationBuilder.CreateIndex(
                name: "ix_person_position_matches_profile_id",
                table: "person_position_matches",
                column: "profile_id");

            migrationBuilder.CreateIndex(
                name: "ix_persons_email",
                table: "persons",
                column: "email",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "ix_profiles_person_id",
                table: "profiles",
                column: "person_id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "job_titles");

            migrationBuilder.DropTable(
                name: "otcs");

            migrationBuilder.DropTable(
                name: "person_position_matches");

            migrationBuilder.DropTable(
                name: "positions");

            migrationBuilder.DropTable(
                name: "profiles");

            migrationBuilder.DropTable(
                name: "persons");
        }
    }
}
