-- Add generic SSO fields to eg_user table
-- This migration adds support for multiple SSO providers

-- Add sso_id column for storing generic SSO provider user IDs
ALTER TABLE eg_user 
ADD COLUMN IF NOT EXISTS sso_id VARCHAR(100);

-- Add sso_type column for storing SSO provider type (EPRAMAAN, DIGILOCKER, etc.)
ALTER TABLE eg_user 
ADD COLUMN IF NOT EXISTS sso_type VARCHAR(20);

-- Create index for faster SSO ID lookups
CREATE INDEX IF NOT EXISTS idx_eg_user_sso_id ON eg_user(sso_id);

-- Create index for SSO type lookups
CREATE INDEX IF NOT EXISTS idx_eg_user_sso_type ON eg_user(sso_type);

-- Create composite index for sso_id and sso_type together (for queries filtering by both)
CREATE INDEX IF NOT EXISTS idx_eg_user_sso_id_type ON eg_user(sso_id, sso_type);

