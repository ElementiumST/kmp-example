import { TestBed } from '@angular/core/testing';
import { KmpBridgeService } from './data-access-kmp-bridge';

describe('KmpBridgeService', () => {
  let service: KmpBridgeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KmpBridgeService);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });
});
