namespace Ostj.Shared.Contracts
{
    public record AuthenticatedUser
    {
        public int UserId { get; set; } = -1;
        public string? Email { get; set; }
        public string? Role { get; set; }
    }
}
