import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatureContacts } from './feature-contacts';

describe('FeatureContacts', () => {
  let component: FeatureContacts;
  let fixture: ComponentFixture<FeatureContacts>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatureContacts]
    }).compileComponents();

    fixture = TestBed.createComponent(FeatureContacts);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
